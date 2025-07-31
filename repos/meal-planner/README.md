[![Build Status](https://travis-ci.org/fzuellich/meal-planner.svg?branch=master)](https://travis-ci.org/fzuellich/meal-planner)

# Meal planner - Prototype

A little prototype for an application idea. Doesn't work properly (yet).

# Setup

1. Create an app on Pinterest.
2. Create a properties file based on the template *src/main/resources/meal_planner.properties.template*.
3. Retrieve the _client id_ and _client secret_ and add it to your configuration *src/main/resources/application-production.properties*.
4. *Make sure to activate the default profile and one of testing or production.*

# Running with Gradle

Execute the Gradle command with a parameter designating the target environment, e.g.:

    /project-base-dir> gradle bootRun

# API Endoints

## Getting informations about boards

**GET** _/boards_ - A list of boards

Returns a list of boards.

**GET** _/board?boardId=?_ - A specific board

Returns information about the board including its pins.

**GET** _/recipes_ - A list of recipes

Returns all the recipes across the list of public boards (see _/boards_).

**GET** _/parse?url=?_ - Parse a recipe

Parse a recipe by its URL and return information about that recipe (ingredients, name, author).

**GET** _/status_ - Check whether the current user is authenticated with Pinterest.