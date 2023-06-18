### Expirable

The package contains some abstractions that help you create and reuse expensive objects across multiple holders. These abstractions are the result of generalization of some ideas borrowed from the implementation of `IHttpClinetFactory` on the `dotnet` platform.

```sh
gradle build
gradle test
```

### How to use

The package exposes the following entities

#### Expirable

Represents an expirable object. There are two ways to cause expiration of an object:
1) pass `ttl` argument and wait for expiration by timer (`TimerTask`)
2) use `try with resources`


#### ExpirableCollection

The main abstraction of the package. This collection manages lifetime of objects. It returns the same object for the same key until the object expires. Users are free to use the object returned from the `ExpirableCollection` for as long as they need, regardless of whether it expires. 
