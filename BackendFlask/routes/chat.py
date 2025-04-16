from flask import Blueprint, request, jsonify
from extensions import limiter
from datetime import datetime
import logging
from clients.openai_client import openai
from clients.redis_client import redis_client
from clients.mongo_client import archive
from config import Config
from utils.jwt_util import decode_token

logger = logging.getLogger(__name__)
chat_bp = Blueprint("chat", __name__, url_prefix="/api/chat")
limiter.limit(Config.CHAT_LIMIT)(chat_bp)

@chat_bp.route("", methods=["POST"])
def chat():
    try:
        # 1) 解析 Token 获取 user_id
        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return jsonify({"error": "Missing or invalid token"}), 401
        token = auth_header.split(" ")[1]
        payload = decode_token(token)
        if not payload or not isinstance(payload, dict):
            return jsonify({"error": "Invalid or expired token"}), 401

        user_id = payload.get("userId")
        if not user_id:
            return jsonify({"error": "Invalid token payload: missing userId"}), 401

        # 2) 解析用户输入
        data = request.get_json()
        if not data or "message" not in data:
            return jsonify({"error": "Missing message"}), 400
        user_message = data["message"].strip()
        if not user_message:
            return jsonify({"error": "Empty message"}), 400

        # 3) Redis 缓存上下文
        redis_key = f"chat:user:{user_id}"
        history = redis_client.lrange(redis_key, 0, -1)[::-1]  # 从 Redis 读取历史记录，倒序读取

        # 构建聊天消息
        messages = [
            {
                "role": "system",
                "content": (
                    "You are a seasoned network security expert. "
                    "You have deep knowledge of network protocols, firewalls, "
                    "intrusion detection systems, vulnerability assessment, "
                    "and incident response. Provide clear, actionable recommendations."
                )
            }
        ]

        # Redis 消息格式化
        for item in history:
            if isinstance(item, bytes):
                item = item.decode("utf-8")
            role, content = item.split(":", 1)
            logger.debug(f"Loaded history role: {role}, content: {content}")
            messages.append({"role": role.lower(), "content": content})

        # 添加用户的新消息
        messages.append({"role": "user", "content": user_message})

        # 4) MongoDB 获取历史（选填）
        user_record = archive.find_one({"user_id": user_id})
        if user_record and "messages" in user_record:
            for msg in user_record["messages"][-10:]:
                messages.insert(-1, {"role": msg["role"], "content": msg["content"]})

        # 5) 调用大模型
        resp = openai.chat.completions.create(
            model="deepseek-chat",
            messages=messages,
            stream=False
        )
        bot_reply = resp.choices[0].message.content

        # 6) 写入 Redis
        redis_client.lpush(redis_key, f"User:{user_message}")
        redis_client.lpush(redis_key, f"Assistant:{bot_reply}")
        redis_client.expire(redis_key, 3600)  # 设置过期时间
        redis_client.ltrim(redis_key, 0, 19)  # 保持最多 20 条记录

        # 7) 写入 MongoDB
        archive.update_one(
            {"user_id": user_id},
            {
                "$push": {
                    "messages": {"role": "user", "content": user_message, "ts": datetime.utcnow()}
                },
                "$setOnInsert": {"created_at": datetime.utcnow()}
            },
            upsert=True
        )
        archive.update_one(
            {"user_id": user_id},
            {"$push": {"messages": {"role": "assistant", "content": bot_reply, "ts": datetime.utcnow()}}}
        )

        # 返回响应
        return jsonify({"reply": bot_reply})

    except Exception as e:
        logger.error(f"Chat error: {e}")
        return jsonify({"error": "Internal server error"}), 500
@chat_bp.route("/history", methods=["GET"])
def get_chat_history():
    try:
        # 获取并解析 token
        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return jsonify({"error": "Missing or invalid token"}), 401
        token = auth_header.split(" ")[1]
        payload = decode_token(token)
        if not payload or not isinstance(payload, dict):
            return jsonify({"error": "Invalid or expired token"}), 401

        user_id = payload.get("userId")
        if not user_id:
            return jsonify({"error": "Invalid token payload: missing userId"}), 401

        # 获取 Redis 聊天记录
        redis_key = f"chat:user:{user_id}"
        history = redis_client.lrange(redis_key, 0, -1)[::-1]  # 倒序取出
        chat_history = []

        for item in history:
            if isinstance(item, bytes):
                item = item.decode("utf-8")
            role, content = item.split(":", 1)
            chat_history.append({"role": role.lower(), "content": content})

        return jsonify({"history": chat_history})

    except Exception as e:
        logger.error(f"History error: {e}")
        return jsonify({"error": "Internal server error"}), 500

