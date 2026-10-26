package com.ecommerce.dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价实体（对应 t_review，用于口碑/评分维度）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_review")
public class Review implements Serializable {

    @Id
    @Column(name = "review_id", length = 32)
    private String reviewId;

    @Column(name = "order_id", length = 32)
    private String orderId;

    @Column(name = "review_score")
    private Integer reviewScore;

    @Column(name = "review_comment_title", length = 255)
    private String reviewCommentTitle;

    @Column(name = "review_comment_message", length = 4000)
    private String reviewCommentMessage;

    @Column(name = "review_creation_date")
    private LocalDateTime reviewCreationDate;

    @Column(name = "review_answer_timestamp")
    private LocalDateTime reviewAnswerTimestamp;
}
