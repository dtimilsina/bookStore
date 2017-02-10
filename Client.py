import threading
import xmlrpclib
import sys
import fileinput
import time

# lookup command
def lookup(server,id):
    print
    answer = server.sample.lookup(id)
    print answer

#search in the database
def search(server,topic):
    print
    answers = server.sample.search(topic)
    for ans in answers:
        print ans

def buy(server,id):
    print
    answer = server.sample.buy(id);
    print answer


    
def run_manually(name):
    server = xmlrpclib.Server(name)
    line = sys.stdin.readline()
    #line = "lookup 12498"

    #process server
    while line:
        line_parts = line.strip().split(' ', 1)
        if len(line_parts) == 2:
            method, params = line_parts
        else:
            method = line_parts[0].lower()

        if method == "lookup":
            try:
                book_id = int(params)
                lookup(server, book_id)
            except ValueError:
                print "Invalid parameter '%s', should be of type integer" % params
        elif method == "search":
            search(server, params)
        elif method == "buy":
            try:
                book_id = int(params)
                buy(server, book_id)
            except ValueError:
                print "Invalid parameter '%s', should be of type integer" % params
        else:
            print "Unknown command '%s', must be lookup, search, or buy" % method

        params = ""
        line = sys.stdin.readline()

def stress_test(name, thread_count, num_requests):
    print "starting test"
    start = time.time()

    threads = []
    for i in range(thread_count):
        try:
            t = threading.Thread(target=run_client, args=(name, num_requests / thread_count))
            t.daemon = True
            threads.append(t)
            t.start()
        except Exception as e:
            print e
            print "Failed to start thread"

    finished_count = 0
    for thread in threads:
        thread.join()
        # print "joined a thread"
        finished_count += 1

    end = time.time()
    print "Started at %d ended at %d diff %d" % (start, end, end-start)

    print "Total time: %f s, time per request: %f s" % ((end - start), (end - start) / float(num_requests))

    print "Done with %d threads" % finished_count

def run_client(name, count):
    server = xmlrpclib.Server(name) 
    for _ in range(count):
        buy(server, 12498)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print "usage: python Client.py <server address> <port number> [-s]"
        sys.exit(1)

    name = "http://"+sys.argv[1]+":"+sys.argv[2]

    if "-s" in sys.argv:
        for i in range(1, 5) + range(5, 51, 5):
            print 'with %d threads' % i
            stress_test(name, i, 500)
            print            
    else:
        run_manually(name)



