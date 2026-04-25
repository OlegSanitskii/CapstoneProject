import Foundation
import SwiftData

struct AuthRepository {
    let context: ModelContext

    func createUser(email: String, password: String, name: String?) {
        let user = User(email: email, password: password, name: name)
        context.insert(user)
    }
}
