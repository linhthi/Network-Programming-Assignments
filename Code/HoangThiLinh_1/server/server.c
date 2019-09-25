/** Server
 * @Fullname: Hoang Thi Linh
 * @Student code: 17020852
 * @Description:
 * Accept connections from client to port 6500
 * Send and recive with Client
*/
#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <unistd.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>

int main() {

    // create the server socket
    int server_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (server_socket < 0) {
        perror("socket");
        exit(1);
    }

    // define the server address
    struct sockaddr_in server_address;
    bzero(&server_address, sizeof(server_address));
    server_address.sin_family = AF_INET;
    server_address.sin_port = htons(6500);
    server_address.sin_addr.s_addr = htonl(INADDR_ANY);

    if ((bind(server_socket, (struct sockaddr*) &server_address, sizeof(server_address))) < 0) {
        perror("bind");
        exit(1);
    }

    if (listen(server_socket, 5) < 0) {
        perror("listen");
        exit(1);
    }

    struct sockaddr_in client_address;
    int newfd;
    int client_address_len = sizeof(client_address);

    newfd = accept(server_socket, (struct sockaddr*) &client_address, &client_address_len);
    if (newfd < 0) {
        perror("accept");
        exit(1);
    }

    char *client_ip = inet_ntoa(client_address.sin_addr);
	uint16_t client_port = ntohs(client_address.sin_port);
    printf("connection from client: %s:%u\n", client_ip, client_port);

    char buff[512];
    int nbytes = read(newfd, buff, sizeof(buff));
    if (nbytes < 0) {
        perror("read");
        exit(1);
    }
    printf("[Message from client]: %s\n", buff);

    strcpy(buff, "Hello client!");
    nbytes = write(newfd, buff, sizeof(buff));
    if (nbytes < 0) {
        perror("write");
        exit;
    }
    printf("[Send client message]: %s\n", buff);

    strcpy(buff, "What is your name?");
    nbytes = write(newfd, buff, sizeof(buff));
    if (nbytes < 0) {
        perror("write");
        exit;
    }
    printf("[Send client message:] %s\n", buff);

    nbytes = read(newfd, buff, sizeof(buff));
    if (nbytes < 0) {
        perror("read");
        exit(1);
    }
    printf("[Message from Client]: %s\n", buff);

    strcpy(buff, "Bye!");
    nbytes = write(newfd, buff, sizeof(buff));
    if (nbytes < 0) {
        perror("write");
        exit;
    }
    printf("[Send client message]: %s\n", buff);

    close(server_socket);


    return 0;

}
