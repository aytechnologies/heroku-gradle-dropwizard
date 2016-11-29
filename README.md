# heroku-dropwizard-gradle
Example Dropwizard project using Gradle that can be deployed on Heroku. 

# Create new Heroku App
After cloning this repository, you need to go inside the folder and create a new app in Heroku to deploy the code to. 

### Login
```
heroku login
Enter your Heroku credentials.
Email: info@aytech.ca
Password (typing will be hidden): 
Logged in as info@aytech.ca
```

### Create an App
```
heroku create heroku-dropwizard-gradle
```

### Add-ons
We use Postgres and RabbitMQ.

```
heroku addons:create heroku-postgresql:hobby-dev
heroku addons:create cloudamqp:lemur
```
### Set Config - Dropwizard port
In order for dropwizard server to connect to a port that heroku assign to your app, you need to set config like this

```
heroku config:set WEB_OPTS='-Ddw.server.applicationConnectors[0].port=$PORT'
```

### Deploy 
Now you can deploy the code to heroku.
```
git push heroku master
```

### Scale worker
By default heroku workers are scalled to 0 (off). You need to scale up your workers to 1 (still free)
```
heroku ps:scale worker=1
```

### Logs
To tail the logs you can use

```
heroku logs -t
```
