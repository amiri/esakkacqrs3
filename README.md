# MyEventSourcedAkkaCQRS 3

This is the fourth learning project.

It is identical to the third, except that the command side is now split into two different levels:
the API now calls the UserCommandAggregate, which then dispatches commands to UserCommandActors. It does
this by either looking them up or creating new instances of them.

The UserCommandActor is now individualized: there is one actor per user, and the path of the actor
is the user UUID. For some reason, I was thinking while developing this that the path had to be identical
to the persistenceId. It does not. So, we can have one actor per user and they can all share the same
persistenceId, which allows our read side to continue as before. This architecture also requires that the
individualized UserCommandActors filter events in ReceiveRecover, to only those events matching their
userId, i.e., path.

To see how it works, open two terminals on your development host (not the vagrant sandbox):

1. In the first terminal, cd into this project and:

```
sbt run
```

2. In the second terminal:

```
curl -XPOST -v --headers "Content-Type: application/json" "http://localhost:2015/user" -d '{"email": "test@test.com"}'
```

3. You will be returned the ID of this new user. In the second terminal you can then run

```
curl -XGET -v http://localhost:2015/user
```

You will be returned a JSON hash, with one key (the user ID) and one value (the user object).

4. Retrieve only this one user:

```
curl -XGET -v http://localhost:2015/user/$id
```

You will be returned only this user's JSON hash.

5. Post a new user:

```
curl -XPOST -v --headers "Content-Type: application/json" "http://localhost:2015/user" -d '{"email": "bug@out.com"}'
```

You will get the ID.

6. Go ahead and play with these two users, editing their email addresses:

```
curl -XPUT -v http://localhost:2015/user/$id '{"email":"money@maker.org"}'
```


So, we addressed the main design flaw in version 3 here. I have not done deletions and "last-modified" timestamps yet,
but those are fairly minor and simple.

I'll get to those in project 5.
