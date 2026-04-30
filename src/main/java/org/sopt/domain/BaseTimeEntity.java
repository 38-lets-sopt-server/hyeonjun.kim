package org.sopt.domain;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass           // 이 클래스 자체는 테이블 안 만들고, 상속한 엔티티에 컬럼만 추가해줘요
@EntityListeners(AuditingEntityListener.class)  // 생성/수정 시간 자동 감지
public abstract class BaseTimeEntity {

    @CreatedDate
    private LocalDateTime createdAt;    // 생성 시간 자동 저장

    @LastModifiedDate
    private LocalDateTime updatedAt;    // 수정 시간 자동 저장

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}