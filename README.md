# JMount

The JMount library can use "Mount Point" (See our specification for more details) to look up the things you want,
and mount it, then return the Mount Point instance for you.

## How to use

### Code

The most simple code example:

```java
public class Something {
    private int a = 1;

    protected void doSomething() {
        // code goes here
    }
}

@MountPoint("Something")
public interface SomethingMP {
    @AccessField
    int a();
    
    void doSomething();
}

public class Usage {
    public static void main(String[] args) {
        NameTransformer transformer; // your transformer here
        Mount mount = MountBuilder.create()
                .classLoader(Thread.currentThread().getContextClassLoader())
                .nameTransformer(transformer)
                .build();
        SomethingMP mp = mount.mount(SomethingMP.class, new Something());
        boolean a = (1 == mp.a()); // true
        mp.doSomething(); // although the underlying method is protected, you can call it even if it is private!
    }
}
```

### Import from JitPack

First, add [JitPack](https://jitpack.io) to your build configuration as the one of the Maven dependency repositories.
* You can see JitPack website to know how to add it.

Now, it's time for you to choose.

There are different implementations of this library, it's up to you to use what version.

Now we have:
* Pure JDK based, brand is "jdk"
* ByteBuddy based, brand is "bytebuddy", **STILL UNDER DEVELOPMENT**

Normally, their artifact ID are in the such format: `jmount-impl-<brand>`.

So the full artifact location is `com.github.SNWCreations:jmount-impl-<brand>:<LATEST_VERSION>`

Remember to replace `<brand>` with the brand you need, and replace `<LATEST_VERSION>` with the latest version!

Version list is available in the tags list in this repository.

## Compile by yourself

Just do `mvn clean install`, everything will done!

## Contributing

Useful contributions are welcome!

Feel free to contribute!

But don't forget to follow [Conventional Commits Specification](https://www.conventionalcommits.org)!

## License

Everything (excluding *The JMount Library Specification* in the `docs/spec` folder of this repository)
 are licensed under Apache 2.0 License.
