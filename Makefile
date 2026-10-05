.PHONY: build test build-image run stop clean logs help clean-db rebuild

PROJECT_NAME := apicustomer
IMAGE_NAME := $(PROJECT_NAME):latest
CONTAINER_NAME := $(PROJECT_NAME)

ifeq ($(origin JAVA_HOME), undefined)
SDKMAN_JAVA_HOME := $(shell readlink -f "$$HOME/.sdkman/candidates/java/current" 2>/dev/null)
ifneq ($(strip $(SDKMAN_JAVA_HOME)),)
export JAVA_HOME := $(SDKMAN_JAVA_HOME)
export PATH := $(JAVA_HOME)/bin:$(PATH)
endif
endif

help:
	@echo "Comandos disponíveis:"
	@echo "  make build       - Build da aplicação Java com Gradle"
	@echo "  make test        - Executar os testes"
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

test:
	@echo "Running tests with Gradle..."
	./gradlew test

build-image:
	@echo "Building Docker image..."
	docker build -t $(IMAGE_NAME) .

run:
	@echo "Starting application..."
	docker compose up -d

stop:
	@echo "Stopping application..."
	docker compose down

clean:
	@echo "Cleaning..."
	docker compose down -v --rmi all
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
	docker compose logs -f

rebuild: clean build-image run
	@echo "Rebuild complete. Application starting..."