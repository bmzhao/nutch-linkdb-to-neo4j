export NAME = neo4j
export LINK_DATA = /Users/brianzhao/Documents/IntellijProjects/Neo4JTest/link_data.txt
export NEO4J_IP = '192.168.99.100'

build:
	./produce-jar.sh
	docker build -t="bmzhao/$(NAME):latest" .

run:
	docker run --name=$(NAME) -e NEO4J_IP=$(NEO4J_IP) -d -v $(LINK_DATA):/root/app/link_data.txt:ro "bmzhao/$(NAME):latest"

push:
	docker push bmzhao/$(NAME):latest

remove:
	docker rm -f $(NAME)

logs:
	docker logs -f $(NAME)