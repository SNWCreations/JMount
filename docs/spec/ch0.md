# Chapter 0 - The Beginning

In the definition in this specification, The JMount Library is a way to "mount" a class
 which is under an unstable package location to a stable interface.

## Why it is designed

Our code know "there is a class with the ability we want".

However, the most terrible thing is that its package is unstable at runtime.

For example, there is a class named "net.minecraft.server.v1_12_R1.EntityPlayer" in a Minecraft 1.12 server,
 we want to call method in it, so we had to import it in our code. However, when we put our binary program into a
 Minecraft 1.16.5 server, the EntityPlayer's full qualified name is
 "net.minecraft.server.v1_16_R3.EntityPlayer" at runtime.

At this time, our program doesn't work. It only know "net.minecraft.server.v1_12_R1.EntityPlayer", it doesn't know
 the class it wants has been relocated!

### Why not call reflect API directly?

I know many Minecraft plugins use Java reflect API to call the classes in that situation.

However, reflection requires security check on EVERY CALL.

And we have to write try-catch block for every reflect code block.

Although we can write utility classes for invoking reflect API, the reflect calls are not in a graceful way we want.

### Why not write adapter for every version?

In my case, these things don't change once between versions, just different package.

So if I do so, I'll have to write too many duplicate codes.

## What does it provide?

A graceful way to call the classes whose package name is unstable at runtime.

## What is its goal?

You provide a Mount Point, we mount the things declared in them for you.

High performance, easy to use.

Graceful.
