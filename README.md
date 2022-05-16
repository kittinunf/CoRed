# CoRed

[![Kotlin](https://img.shields.io/badge/kotlin-1.6.21-blue.svg)](http://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.kittinunf.cored/cored?label=Maven%20Central)](https://search.maven.org/artifact/com.github.kittinunf.cored/cored)

CoRed is Redux-like implementation that maintains the benefits of Redux's core idea without the
boilerplate. No more action types, action creators, switch statements or complicated setup. It is
Kotlin and it has coroutine supported right out-of-the-box. Also, yes, it is Kotlin Multiplatform
supported (https://kotlinlang.org/docs/mpp-intro.html)

## Features

CoRed is an opinionated way to setup the Redux implementation. Why we need CoRed? Redux is an
amazing state management tool. However, some might find it to be too complicated or hard to
use/understand, because the plain vanilla Redux-like implementation is quite boilerplate-y. There is
an even
[page](https://redux.js.org/recipes/reducing-boilerplate/) from the official Redux.js on how to
reduce the boilerplate. That's why we develop CoRed.

In CoRed's implementation, we are trying to solve the same problem with original Redux by
introducing the more friendly API, then translating that into Kotlin in order to be a bit more
accessible to mobile dev to use in their projects.

## Installation

Add mavenCentral into your dependencies' repositories configuration

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    // if you are working on JVM or Android only project
    implementation("com.github.kittinunf.cored:cored-jvm:«version»") //for JVM support

    // if you are working in KMM project
    implementation("com.github.kittinunf.cored:cored:«version»") //for Kotlin Multiplatform support
}
```

## How to set this up?

Good question. Let's try to set up a minimal example
with [HashEngine](./cored/src/commonMain/kotlin/com/github/kittinunf/cored/engine/HashEngine.kt) with
an ability to show a list data from the network.

Assuming that we have a Repository class that already connects to the API somewhere,
eg. [Comments](http://jsonplaceholder.typicode.com/comments), we can use it to fetch the data for
our store.

```kotlin
interface CommentRepository {
    suspend fun getComments(): List<String>
}
```

With the concrete class implements above interface with your preferred network engine

```kotlin
class CommentRepositoryImpl : CommentRepository 
```

Redux with CoRed implementation (the setup part should be under 35~ lines)

```kotlin
// State definition for you application
class CommentsState(val isLoading: Boolean, val comments: List<String>? = null)

// Actions
object Load
class SetComments(val comments: List<String>?)

val repository: CommentRepositoryImpl // get repository somewhere e.g. manually create, DI, or 3rd party library

val store = Store(
    scope = viewScope,
    initialState = CommentsState(),
    reducers = mapOf(
        SetComments::class to Reducer { currentState: CommentsState, action: SetComments -> // This reducer is connected with SetComments action by using SetComments::class as a Key
            currentState.copy(comments = action.comments)
        }
    ),
    middlewares = mapOf(
        Load::class to Middleware { _: Order, store: Store, state: CommentsState, _: Load -> // This middleware is connected with Load action by using Load::class as a Key
            if (state.isLoading) return@Middleware
            scope.launch {
                val result = repository.getComments()
                if (result.isSuccess) {
                    store.dispatch(SetComments(result.value))
                } else {
                    store.dispatch(SetComments(null))
                }
            }
        }
    )
)

```

Usage

```kotlin

// in Coroutine scope - you can observe state changes with `collect`
store.states
    .collect {
        /** This should return { comments : [ {
        "postId": 1,
        "id": 1,
        "name": "id labore ex et quam laborum",
        "email": "Eliseo@gardner.biz",
        "body": "laudantium enim quasi est quidem magnam voluptate ipsam eos\ntempora quo necessitatibus\ndolor quam autem quasi\nreiciendis et nam sapiente accusantium"
        }, ... ] }
         **/
        println(it)
    }

// dispatch an action 
store.dispatch(Load)
```

or you can use the simplified store creation version like;

```kotlin
val store = Store(
    scope = viewScope,
    initialState = CommentsState(),
    reducers = setOf(
        reducer { currentState: CommentsState, action: SetComments -> // This reducer is connected with SetComments action by using SetComments::class as a Key
            currentState.copy(comments = action.comments)
        }
    ),
    middlewares = setOf(
        middleware { _: Order, store: Store, state: CommentsState, _: Load -> // This middleware is connected with Load action by using Load::class as a Key
            if (state.isLoading) return@Middleware
            scope.launch {
                val result = repository.getComments()
                if (result.isSuccess) {
                    store.dispatch(SetComments(result.value))
                } else {
                    store.dispatch(SetComments(null))
                }
            }
        }
    )
)
```

It uses the reified function with `T::class` for action type like `action: SetComments` or `_: Load` automatically under the hood.

For documentation, check more details in the [README](./cored/README.md)

For example, check more tests in the
[commonTest](./cored/src/commonTest/kotlin/com/github/kittinunf/cored/StoreAdapterTest.kt) folder.

## Credits

CoRed is brought to you by [contributors](https://github.com/kittinunf/CoRed/graphs/contributors).

## Licenses

CoRed is released under the [MIT](https://opensource.org/licenses/MIT) license.
