import Foundation
import SwiftData

@Model
final class User {
    var id: UUID
    var email: String
    var password: String
    var name: String?

    init(
        id: UUID = UUID(),
        email: String,
        password: String,
        name: String? = nil
    ) {
        self.id = id
        self.email = email
        self.password = password
        self.name = name
    }
}
