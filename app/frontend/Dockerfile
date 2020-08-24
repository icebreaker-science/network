## Build app ##

FROM node:12.16-slim AS builder
WORKDIR /build/

COPY package.json .
COPY package-lock.json .
RUN npm set progress=false && \
    npm config set depth 0 && \
    npm  --quiet ci

RUN npm install -g bower
RUN apt update
# Bower needs git
RUN apt install -y git
COPY bower.json .
RUN bower install --allow-root

COPY . .


## Serve via nginx ##

FROM nginx:stable as server

COPY --from=builder /build /app
RUN rm /etc/nginx/conf.d/default.conf
COPY docker_resources/nginx-icebreaker.conf /etc/nginx/conf.d/icebreaker.conf

EXPOSE 12250
