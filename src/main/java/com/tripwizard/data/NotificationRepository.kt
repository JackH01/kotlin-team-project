package com.tripwizard.data

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllNotificationsStream(): Flow<List<Notification>>
    suspend fun insertNotification(notification: Notification)
    suspend fun insertNotificationList(notifications: List<Notification>)
    suspend fun deleteNotification(notification: Notification)
    suspend fun updateNotification(notification: Notification)
}