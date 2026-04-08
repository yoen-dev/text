package com.nourishwell.utils

import com.nourishwell.models.User
import com.nourishwell.models.UserRole
import org.mindrot.jbcrypt.BCrypt

/**
 * In-memory user store.
 * Replace the `users` MutableList with database calls (Exposed ORM recommended)
 * when you're ready to add persistence.
 *
 * To switch to a database:
 *   1. Add Exposed + JDBC driver to build.gradle.kts
 *   2. Replace findByEmail / save / findById with Exposed DSL queries
 *   3. Remove the hardcoded backdoor users (or keep them behind an env flag)
 */
object UserStore {

    // ── Backdoor test accounts ─────────────────────────────────────────────────
    //    email: test@user.com  / test1234  → subscriber  → /dashboard.html
    //    email: test@pro.com   / test1234  → professional → /pro_dashboard.html
    private val users: MutableList<User> = mutableListOf(
        User(
            id = "usr_test_subscriber",
            email = "test@user.com",
            passwordHash = BCrypt.hashpw("test1234", BCrypt.gensalt()),
            firstName = "Rose",
            lastName = "Campbell",
            role = UserRole.subscriber,
            backdoor = true
        ),
        User(
            id = "usr_test_pro",
            email = "test@pro.com",
            passwordHash = BCrypt.hashpw("test1234", BCrypt.gensalt()),
            firstName = "Dr.",
            lastName = "Rivera",
            role = UserRole.professional,
            backdoor = true
        )
    )

    fun findByEmail(email: String): User? =
        users.find { it.email.equals(email, ignoreCase = true) }

    fun findById(id: String): User? =
        users.find { it.id == id }

    fun save(user: User) {
        users.add(user)
    }

    fun exists(email: String): Boolean =
        findByEmail(email) != null
}
