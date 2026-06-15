# Estagio 1: build do Angular.
# node:lts-alpine porque a imagem so precisa do toolchain pra gerar os estaticos.
FROM node:lts-alpine AS build
WORKDIR /app
COPY package*.json ./
# npm ci usa o package-lock.json (build reprodutivel), diferente do npm install.
RUN npm ci
COPY . .
# build de producao (defaultConfiguration do angular.json).
RUN npm run build

# Estagio 2: serve so os estaticos com nginx, sem o node nem node_modules.
FROM nginx:alpine
# o builder @angular/build:application gera em dist/<projeto>/browser (Angular 17+).
COPY --from=build /app/dist/bantads-ui/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
