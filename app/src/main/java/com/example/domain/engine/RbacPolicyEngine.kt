package com.example.domain.engine

import com.example.domain.model.Permission
import com.example.domain.model.Role

class RbacPolicyEngine {

    private val rolePermissions: Map<Role, Set<Permission>> = mapOf(
        Role.SUPER_ADMIN to Permission.entries.toSet(),
        Role.ADMIN to setOf(
            Permission.USERS_READ,
            Permission.USERS_UPDATE,
            Permission.USERS_SUSPEND,
            Permission.REWARDS_READ,
            Permission.REWARDS_ADJUST,
            Permission.WITHDRAWALS_READ,
            Permission.WITHDRAWALS_APPROVE,
            Permission.WITHDRAWALS_REJECT,
            Permission.CAMPAIGNS_CREATE,
            Permission.CAMPAIGNS_UPDATE,
            Permission.CAMPAIGNS_DELETE,
            Permission.FRAUD_READ,
            Permission.FRAUD_REVIEW,
            Permission.SETTINGS_UPDATE,
            Permission.AUDIT_READ,
            Permission.SUPPORT_READ,
            Permission.SUPPORT_REPLY,
            Permission.SUPPORT_RESOLVE
        ),
        Role.FINANCE to setOf(
            Permission.USERS_READ,
            Permission.REWARDS_READ,
            Permission.REWARDS_ADJUST,
            Permission.WITHDRAWALS_READ,
            Permission.WITHDRAWALS_APPROVE,
            Permission.WITHDRAWALS_REJECT,
            Permission.AUDIT_READ
        ),
        Role.MODERATOR to setOf(
            Permission.USERS_READ,
            Permission.USERS_UPDATE,
            Permission.USERS_SUSPEND,
            Permission.CAMPAIGNS_CREATE,
            Permission.CAMPAIGNS_UPDATE,
            Permission.FRAUD_READ,
            Permission.FRAUD_REVIEW,
            Permission.SUPPORT_READ,
            Permission.SUPPORT_REPLY
        ),
        Role.SUPPORT to setOf(
            Permission.USERS_READ,
            Permission.REWARDS_READ,
            Permission.WITHDRAWALS_READ,
            Permission.FRAUD_READ,
            Permission.SUPPORT_READ,
            Permission.SUPPORT_REPLY,
            Permission.SUPPORT_RESOLVE
        ),
        Role.ANALYST to setOf(
            Permission.USERS_READ,
            Permission.REWARDS_READ,
            Permission.WITHDRAWALS_READ,
            Permission.FRAUD_READ,
            Permission.AUDIT_READ
        ),
        Role.USER to emptySet()
    )

    fun hasPermission(role: Role, permission: Permission): Boolean {
        val permissions = rolePermissions[role] ?: emptySet()
        return permissions.contains(permission)
    }

    fun hasPermission(userRoleStr: String, permission: Permission): Boolean {
        val role = Role.fromString(userRoleStr)
        return hasPermission(role, permission)
    }

    fun requirePermission(role: Role, permission: Permission) {
        if (!hasPermission(role, permission)) {
            throw SecurityException("Forbidden: Role '${role.roleName}' lacks required permission '${permission.permissionKey}'")
        }
    }

    fun requirePermission(userRoleStr: String, permission: Permission) {
        val role = Role.fromString(userRoleStr)
        requirePermission(role, permission)
    }

    fun getPermissionsForRole(role: Role): Set<Permission> {
        return rolePermissions[role] ?: emptySet()
    }

    fun getPermissionsForRole(roleStr: String): Set<Permission> {
        val role = Role.fromString(roleStr)
        return getPermissionsForRole(role)
    }
}
