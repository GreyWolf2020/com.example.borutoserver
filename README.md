# Boruto Server
<p align="center">
  <img src="https://i.postimg.cc/4yX4vXCZ/Boruto.png" href="">
</p>

Boruto Server is the back-end application for [Boruto Application](https://github.com/GreyWolf2020/BorutoApp).
The application lists popular characters from Boruto, a famous Japanese manga.
The application was built with Ktor.

## Table of Content
- [Installation](#installation)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)

## Installation
1. Get the repository
```bash
    git clone https://github.com/GreyWolf2020/com.example.borutoserver.git
```
2. Install dependencies:
```bash
    ./gradlew build
```
## Usage
Execute this instruction to run the application:
```bash
    ./gradlew run
```

## API Endpoints
| HTTP Verbs | Endpoints | Action                                                                                                                                |
| ---   | - |---------------------------------------------------------------------------------------------------------------------------------------|
| GET | / | Welcome message                                                                                                                       |
| GET | /boruto/heroes | To retrieve a list of heroes. Pagination is powered exclusively by the <mark style="background-color: #D3D3D3">page</mark> parameter. |
| GET | /boruto/heroes/search | To retreive a list of searched heroes                                                                                                 |
