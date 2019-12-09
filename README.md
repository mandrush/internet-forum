# Internet Forum
This small REST API app is a simple internet forum (like 4chan, for example). You can submit new topics, create replies to those topics, edit them and delete them. You can also view the most active posts and look for top replies.

There is no client and frontend at the moment, so all communication is done via HTTP requests (use curl or whatever you like).
## Stack:
* Scala
* Akka HTTP
* Slick
* PostgreSQL

# Usage
## Running the app
You need to install `sbt`, `docker` and `docker-compose`.

First, build the fat jar for the app by running this command in the root directory:

```
sbt assembly
```

Then, in order to run the app, run
```$xslt
docker-compose up --build 
```
The service will then be deployed at `http://localhost:8080`
## Your configuration
If you wish to reconfigure the app (e.g. set other values to fields like `maxPaginationLimit`), please edit the `yourconfig.conf` file in the `/run/app` folder. This configuration file is copied into the docker container and is the main source of configuration for the app.

App configuration fields and their default values:
*  __minimumLength__       : 1 
*  __maxNickLength__       : 21
*  __maxEmailLength__      : 254 
*  __maxContentLength__    : 400
*  __maxTopicLength__      : 80
*  __maxPaginationLimit__  : 7 
*  __host__                : 0.0.0.0
*  __port__                : 8080

## Request paths
### POST /create-post
Create new posts with topic.

These requests need to contain: __topic__, __content__, __nickname__, __email__.

Successful requests return a JSON with this post's data. 

**Note**: it's important to look at the responses, as for example here, they will include secret hashes, which then are needed to edit and delete posts and replies. 

### POST /create-reply?post_id={post_id}
Create new replies to corresponding posts.

These requests need to contain __content__, __nickname__, __email__

You also need to know the id of the post to which you are replying. You will obtain it by looking at the **"id"** field in the JSON returned by **/create-post**.

### POST /edit-post?post_id={post_id}
Edit a given post.

The requests needs to contain: __newContent__, __secret__, where __secret__ is obtained by looking at the JSON response returned after creating a post with the given id.

You will not be able to edit the post without the secret.

### POST /edit-reply?reply_id={reply_id}
Edit a given reply.

The requests needs to contain: __newContent__, __secret__, where __secret__ is obtained by looking at the JSON response returned after creating a reply with the given id.

### POST /delete-post?post_id={post_id}
Delete a given post.
__WARNING__ This also deletes all of its replies.

The request needs to contain: __secret__

### POST /delete-reply?reply_id={reply_id}
Delete a given reply. 

The request needs to contain __secret__

### GET /top-posts?limit={limit}&offset={offset}
Get the list of the most active posts, starting with the most active. The most active post is the one with the most recent child reply.

__limit__ describes how many posts will be there in the returned list. If this limit exceeds the configured __maxPaginationLimit__, the number of posts returned will be determined by __maxPaginationLimit__.

__offset__ describes how many top posts you want to skip. Say, you want to view top 2, top3 and top 4 active posts, skipping top 1. You would then send a GET to:
```$xslt
/top-posts?limit=3&offset=1
```

### GET /reply-list?post_id={post_id}&reply_id={reply_id}&before={before}&after={after}

Get the reply list, starting from the oldest reply. 
* post_id - the id of the post from which you want to read the replies,
* reply_id - the "middle" reply id from the list - more on that in a moment,
* before - how many older replies should be before the selected reply_id
* after - how many newer replies should be after the selected reply_id

You select a reply, and in addition, you tell the service how many older (before) and newer (after) replies should be returned. This list is always sorted by date, starting from the oldest reply.

In order to avoid returning the entire reply list, __before__ and __after__ params are limited by __maxPaginationLimit__. If `before + after + 1 > maxPaginationLimit`, __maxPaginationLimit__ takes precedence. The returned list size is then scaled so that it matches the size described by __maxPaginationLimit__. 

If such scaling occurs, the position of __reply_id__ reply is also proportionally scaled. If it were to be at the middle of the list, it will still be there.


## Troubleshooting

* `postgres appears to contain a database` warning during `docker-compose --build`
    
    If you see such warning during the build process, stop and remove the containers created by `docker-compose` and remove the docker image for `postgres:11.6` and the app.