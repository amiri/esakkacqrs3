# MyEventSourcedAkkaCQRS 3

This is the third learning project.

It is identical to the second, except that we can handle the creation and
editing of multiple users, now.

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

Note that the ID and timestamp remain the same when you do a GET request on all the users or the one.

There is a design flaw in this application: the "command side," i.e., the UserCommandActor PersistentActor, duplicates the work of the "query side," i.e., the UserQueryActor PersistentView, in that it stores the entire state as well. So that we could use the command actor to answer our questions. We're basically doubling our memory requirements.

Another problem is that we don't address deletions.

We will tackle both these in project 4.
