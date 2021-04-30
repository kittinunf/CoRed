# CoRed

CoRed is Redux-like implementation that maintains the benefits of Redux's core idea without the
boilerplate. No more action types, action creators, switch statements or complicated setup. It is Kotlin and it has
coroutine supported right out-of-the-box. Also, yes, it is Kotlin
Multiplatform supported (https://kotlinlang.org/docs/mpp-intro.html)

## Features

CoRed is opinionated way to setup the Redux implementation. Redux is an amazing state management
tool. However, we feel you because the plain vanilla Redux-like implementation is quite
boilerplate-y. There is even a [page](https://redux.js.org/recipes/reducing-boilerplate/) from the
official Redux.js on how to reduce the boilerplate. We try to translate that into Kotlin and make it suitable for mobile developers to use it in their projects.

## Installation

TBD

## How to set this up?

Good question. Let's try to set up a minimal example with [StoreAdapter](./cored/src/commonMain/kotlin/com/github/kittinunf/cored/StoreAdapter.kt) with an ability to show a list data from the network.

Assuming that we have a Repository class that already connects to the API somewhere, eg. [Comments](http://jsonplaceholder.typicode.com/comments)

```kotlin
interface CommentRepository {
    suspend fun getComments(): List<String>
}
```

Redux with CoRed implementation (the setup part should be under 35~ lines)

```kotlin
class CommentsState(val comments: List<String>? = null) : State

object Load : Identifiable
class SetComments(val comments: List<String>?) : Identifiable

val repository: CommentRepository // get repository somewhere e.g. manually create, DI, or 3rd party library

val store = createStore(
    scope = viewScope,
    initialState = CommentsState(),
    reducers = mapOf(
        "SetComments" to Reducer { currentState: CommentsState, action: SetComment ->
            currentState.copy(comments = action.comments)
        }
    ),
    middlewares = mapOf(
        "Load" to Middleware<CommentsState, Load, CommentRepository> {
            override fun process(order: Order, store: Store, state: CommentsState, action: Load) {
                if (order == Order.AfterReduced) {
                    scope.launch {
                        val result = repository.getComments()
                        if (result.isSuccess) {
                            store.dispatch(SetComments(result.value))
                        } else {
                            store.dispatch(SetComments(null))
                        }
                    }
                }
            }

            override val environment: CommentRepository = repository
        }
    )
)

// store is ready to be used (dispatched)
store.dispatch(Load)
```

Please check it out now!

## Credits

CoRed is brought to you by [contributors](https://github.com/kittinunf/CoRed/graphs/contributors).

## Licenses

CoRed is released under the [MIT](https://opensource.org/licenses/MIT) license.
