// First Commit - Ahmed Abdulrahman Ahmed Ali Gamel - B032320114
// git commit -m "Add Notification repository - Ahmed B032320114"
package com.smartcampus.notification.repository;

import com.smartcampus.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByType(String type);
    List<Notification> findByStatus(String status);
}