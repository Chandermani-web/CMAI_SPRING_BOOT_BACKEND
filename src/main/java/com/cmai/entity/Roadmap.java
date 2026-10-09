package com.cmai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "roadmaps", indexes = @Index(name = "idx_roadmaps_user", columnList = "user_id"))
public class Roadmap extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_role", nullable = false)
    private String targetRole;

    @Column(name = "target_package", nullable = false)
    private String targetPackage;

    @Column(name = "use_resume", nullable = false)
    private boolean useResume;

    /** Snapshot of the resume details used to personalise this roadmap (null when not used). */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> resume;

    /** The generated roadmap: title, duration, level and modules (with YouTube / article links). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "roadmap_data", nullable = false)
    private Map<String, Object> roadmapData;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public String getTargetPackage() { return targetPackage; }
    public void setTargetPackage(String targetPackage) { this.targetPackage = targetPackage; }
    public boolean isUseResume() { return useResume; }
    public void setUseResume(boolean useResume) { this.useResume = useResume; }
    public Map<String, Object> getResume() { return resume; }
    public void setResume(Map<String, Object> resume) { this.resume = resume; }
    public Map<String, Object> getRoadmapData() { return roadmapData; }
    public void setRoadmapData(Map<String, Object> roadmapData) { this.roadmapData = roadmapData; }
}
