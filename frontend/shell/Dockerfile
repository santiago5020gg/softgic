# Multi-stage: build de la SPA con Rspack + runtime nginx.
FROM node:20 AS build
WORKDIR /app
COPY package.json .npmrc ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 8080
