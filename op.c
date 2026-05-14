#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

int verificarPar(int numero) {
int dobro = numero * 2;
if(numero % 2 == 0) {
printf("Numero eh par\n");
} else {
printf("Numero eh impar\n");
}printf("Dobro do numero:\n");
printf("%d\n", dobro);
return  dobro;
}

int main() {
int resultado = verificarPar(7);
    return 0;
}
