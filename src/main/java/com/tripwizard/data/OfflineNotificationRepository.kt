package com.tripwizard.data;

import kotlinx.coroutines.flow.Flow

class OfflineNotificationRepository(private val notificationDao: NotificationDao) :
    NotificationRepository {
    override fun getAllNotificationsStream(): Flow<List<Notification>> =
        notificationDao.getAllNotifications()

    override suspend fun insertNotification(notification: Notification) =
        notificationDao.insert(notification)

    override suspend fun insertNotificationList(notifications: List<Notification>) =
        notificationDao.insertList(notifications)

    override suspend fun deleteNotification(notification: Notification) =
        notificationDao.delete(notification)

    override suspend fun updateNotification(notification: Notification) =
        notificationDao.update(notification)
}
