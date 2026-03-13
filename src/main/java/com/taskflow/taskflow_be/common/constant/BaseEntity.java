package com.taskflow.taskflow_be.common.constant;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity implements Serializable {
    @CreatedDate
    @Column(name="CREATE_DATE")
    private Date createDate;

    @Column(name="CREATE_USER")
    @CreatedBy
    private String createUser;

    @Column(name="UPDATE_DATE")
    @LastModifiedDate
    private Date updateDate;

    @Column(name="UPDATE_USER")
    @LastModifiedBy
    private String updateUser;

    @Column(name="SYNC_SOURCE")
    private String syncSource;

    @Column(name="SYNC_DATE")
    private Date syncDate;
}
