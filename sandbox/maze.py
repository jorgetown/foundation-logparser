import random

# constants for the matrix size
MAX_ROWS = 10
MAX_COLS = 10
HUNTER_ID = 1
PRAY_ID = 3
      
def find_target(m):
    print m.m
    print "Mouse @ ", m.trace()
    print "Cheese @ ", m.gotcha()
    scan_cols(m, 't')
    scan_cols(m, 'b')
    print m.m

def scan_cols(m, v):
    if (m.success()):
        return
    if m.move(v) != False:
        print m.trace()
        scan_row(m, 'l')
        scan_row(m, 'r')
        scan_cols(m, v)
            
def scan_row(m, h):
    if (m.success()):
        print "SUCCESS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        print "Target found @ ", m.gotcha()
        return m.m
    else:
        if m.move(h) != False:
            print m.trace()
            scan_row(m, h)
                
class Maze:
    # init maze with default values if they aren't given
    def __init__(self, r=MAX_ROWS, c=MAX_COLS):
        self.rows = r
        self.cols = c
        # setup matrix w/ 0's
        self.m = self.zeros(r, c)

    # convenient zero'ing hack for multidimensional matrices
    def zeros(self, *shape):
        if len(shape) == 0:
            return 0
        car = shape[0]
        cdr = shape[1:]
        return [self.zeros(*cdr) for i in range(car)]

    # return "random" x, y coordinates to populate participants in the matrix
    def random_pos(self, rows, cols):
        x = random.randrange(rows-1)
        y = random.randrange(cols-1)
        return x, y

    # populate maze
    def initialize(self):
        # set hunter starting position
        self.hunter_x, self.hunter_y = self.random_pos(self.rows, self.cols)
        self.m[self.hunter_y][self.hunter_x] = HUNTER_ID
        # set pray starting position
        self.pray_x, self.pray_y = self.random_pos(self.rows, self.cols)
        while (not self.is_empty(self.pray_x, self.pray_y)):
            self.pray_x, self.pray_y = self.random_pos(self.rows, self.cols)
        self.m[self.pray_y][self.pray_x] = PRAY_ID

    def is_empty(self, col, row):
        return self.m[row][col] == 0

    def peek_at(self, col, row):
        return self.m[row][col]

    def success(self):
        return self.m[self.hunter_y][self.hunter_x] == PRAY_ID

    def move_to(self, col, row):
        if (not self.success()):
            self.m[row][col] = HUNTER_ID

    def move(self, direction):
        if (direction == 'l'):
            if (self.hunter_x > 0):
                # clear current position
                self.m[self.hunter_y][self.hunter_x] = 0
                # update to new position
                self.hunter_x = self.hunter_x-1
                self.move_to(self.hunter_x, self.hunter_y)
                return True
            else:
                return False
            
        if (direction == 'r'):
            if (self.hunter_x < self.cols-1):
                # clear current position
                self.m[self.hunter_y][self.hunter_x] = 0
                # update to new position
                self.hunter_x = self.hunter_x+1
                self.move_to(self.hunter_x, self.hunter_y)
                return True
            else:
                return False

        if (direction == 'b'):
            if (self.hunter_y < self.rows-1):
                # clear current position
                self.m[self.hunter_y][self.hunter_x] = 0
                # update to new position
                self.hunter_y = self.hunter_y+1
                self.move_to(self.hunter_x, self.hunter_y)
                return True
            else:
                return False

        if (direction == 't'):
            if (self.hunter_y > 0):
                # clear current position
                self.m[self.hunter_y][self.hunter_x] = 0
                # update to new position
                self.hunter_y = self.hunter_y-1
                self.move_to(self.hunter_x, self.hunter_y)
                return True
            else:
                return False

    # convenience methods
    def trace(self):
        return '@x=%(x)d and y=%(y)d' % {"x": self.hunter_x, "y": self.hunter_y}

    def gotcha(self):
        return '@x=%(x)d and y=%(y)d' % {"x": self.pray_x, "y": self.pray_y}


m = Maze()
m.initialize()
find_target(m)