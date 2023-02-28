# SmartBlogAPI
A blog API where users can make posts and comment on tWelcome to our blogging platform, where you can create an account and start sharing your thoughts and experiences with the world. Whether you're a seasoned writer or just starting out, our user-friendly interface makes it easy to create and publish posts in no time.

Our platform is designed to foster community and engagement, allowing other users to comment on your posts and share their own perspectives. You can also follow other bloggers, stay up-to-date with their latest posts, and build a network of like-minded individuals.

As an admin you have additional privileges that allow you to manage the platform and ensure that it runs smoothly. For example, you can make comments that do not violate our community guidelines.

If you're an admin, you have even greater powers, such as the ability to delete posts created by other users. We entrust you with this responsibility because we believe that you have the best interests of the community at heart and will use your powers wisely and fairly.

Above all, we believe in the power of words to inspire, inform, and connect people from all walks of life. We hope that our blogging platform will become a space where you can express yourself freely and connect with others who share your passions and interests. Thank you for joining us on this journey!

## Controller

### AuthController (api/v1/auth)

|    Method     |       Path         |    Description                           |
| ------------- |    -------------   |   -------------                          |
|    Post       |       /signin      |   Endpoint to register on this blog      |
|    Post       |       /signup      |   Endpoint to login to this blog         |


### PostController(api/v1/post)

|    Method     |       Path           |    Description                           |
| ------------- |    -------------     |   -------------                          |
|    Post       |    /api/v1/post      |   Endpoint to make a post                |
|    Get        |    /api/v1/post      |   Endpoint to get all posts              |
|    Get        |         /{id}        |   Endpoint to get a post by id           |
|    Put        |         /{id}        |   Endpoint to edit a post                |
|    Delete     |         /{id}        |   Endpoint to delete a post              |


### CommentController(api/v1)

|    Method     |                Path                    |            Description                       |
| ------------- |               -------------            |           -------------                      |
|    Post       |     /posts/{postId}/comments           |     Endpoint to make a comment on a post     |
|    Get        |     /posts/{postId}/comments           |     Endpoint to get all comments by post     |
|    Get        |     /posts/{postId}/comments/{id}      |     Endpoint to get a comment by id          |
|    Put        |     /posts/{postId}/comments/{id}      |     Endpoint to update a comment by id       |
|    Delete     |     /posts/{postId}/comments/{id}      |     Endpoint to delete a comment by id       |

## Service


