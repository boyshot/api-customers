.PHONY: build build-image run stop clean logs help clean-db

PROJECT_NAME := apicustomer
IMAGE_NAME := $(PROJECT_NAME):latest
CONTAINER_NAME := $(PROJECT_NAME)

help:
	@echo "Comandos disponíveis:"
	@echo "  make build       - Build da aplicação Java com Gradle"
	@echo "  make build-image - Build da imagem Docker"
	@echo "  make run         - Iniciar a aplicação com Docker Compose"
	@echo "  make stop        - Parar a aplicação"
	@echo "  make clean       - Limpar builds e containers"
	@echo "  make clean-db    - Deletar banco de dados H2"
	@echo "  make logs        - Visualizar logs"
	@echo "  make rebuild     - Rebuild completo (clean + build + run)"

build:
	@echo "Building application with Gradle..."
	./gradlew build -x test

build-image:
	@echo "Building Docker image..."
	docker build -t $(IMAGE_NAME) .

run:
	@echo "Starting application..."
	docker-compose up -d

stop:
	@echo "Stopping application..."
	docker-compose down

clean:
	@echo "Cleaning..."
	docker-compose down -v --rmi all
	./gradlew clean

clean-db:
	@echo "Deleting H2 database files..."
	@if [ -f data/customerdb.mv.db ]; then \
		rm -f data/customerdb.mv.db data/customerdb.trace.db; \
		echo "H2 database deleted successfully."; \
	else \
		echo "H2 database files not found."; \
	fi

logs:
	@echo "Showing logs..."
	docker-compose logs -f

rebuild: clean build-image run
	@echo "Rebuild complete. Application starting..."