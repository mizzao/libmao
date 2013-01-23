CC=gfortran
CFLAGS=-shared -Wall -fPIC

all: mvnpack.so

mvnpack.so: mvndstpack.f mvnexppack.f
	$(CC) $(CFLAGS) mvndstpack.f mvnexppack.f -o mvnpack.so
	cp mvnpack.so ../../../target/classes/

clean:
	rm -f mvnpack.so
