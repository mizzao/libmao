CC=gfortran
SOURCES=mvndstpack.f mvnexppack.f
OBJECTS=$(SOURCES:.f=.o)
LDFLAGS=-Wall -shared
CFLAGS=-Wall -fPIC

all: mvnpack.so

mvnpack.so: $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

.f.o:
	$(CC) $(CFLAGS) -c $<

clean:
	rm -f *.o mvnpack.so

install:
	cp mvnpack.so ../resources
