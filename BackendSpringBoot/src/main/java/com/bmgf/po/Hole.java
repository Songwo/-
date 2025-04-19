package com.bmgf.po;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "Hole")
public class Hole {
    @Id
    private String id;
    private String cve_Id;
    private String title;
    private String description;
    private String severity;
    private String cvss_score;
    private Date published_date;
    private List<String> affected_versions;
    private String solutions;
    private Date created_at;
    private Date updated_at;
    private String type;
}
