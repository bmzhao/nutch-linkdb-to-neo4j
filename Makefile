export NAME = neo4j
export LINK_DATA = /Users/brianzhao/Documents/IntellijProjects/Neo4JTest/link_data.txt
#enter NEO 4J IP Address Here
export NEO4J_IP = ''
#enter NEO 4J IP Password Here
export NEO4J_PASS = ''

build:
	./produce-jar.sh
	docker build -t="bmzhao/$(NAME):latest" .

run:
	docker run -d --name=$(NAME) -e NEO4J_IP=$(NEO4J_IP) -e NEO4J_PASS=$(NEO4J_PASS) -v $(LINK_DATA):/root/app/link_data.txt:ro "bmzhao/$(NAME):latest"

push:
	docker push bmzhao/$(NAME):latest

remove:
	docker rm -f $(NAME)

logs:
	docker logs -f $(NAME)