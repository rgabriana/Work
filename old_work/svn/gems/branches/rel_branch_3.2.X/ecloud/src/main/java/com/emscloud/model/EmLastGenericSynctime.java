package com.emscloud.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "em_last_generic_synctime", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmLastGenericSynctime implements java.io.Serializable {

    private static final long serialVersionUID = -6929028722800793934L;
    private Long id;
    private Long emId;
    private Date lastSyncAt;
    private String syncOperation;

    // private Date last15MinFloorEcTableTimeStamp;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "em_last_generic_synctime_seq")
    @SequenceGenerator(name = "em_last_generic_synctime_seq", sequenceName = "em_last_generic_synctime_seq")
    @Column(name = "id")
    @XmlElement(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "em_id", nullable = false)
    @XmlElement(name = "emId")
    public Long getEmId() {
        return emId;
    }

    public void setEmId(Long emId) {
        this.emId = emId;
    }

    @Column(name = "last_sync_at")
    @XmlElement(name = "lastSyncAt")
    public Date getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(Date lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    @Column(name = "sync_operation_name")
    @XmlElement(name = "syncOperation")
    public String getSyncOperation() {
        return syncOperation;
    }

    public void setSyncOperation(String syncOperation) {
        this.syncOperation = syncOperation;
    }

}
