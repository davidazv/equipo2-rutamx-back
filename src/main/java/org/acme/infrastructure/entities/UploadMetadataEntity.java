package org.acme.infrastructure.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "upload_metadata")
public class UploadMetadataEntity {

    @Id
    @Column(name = "table_name", length = 50)
    private String tableName;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}
