# Convenience scripts for use on Windows to batch-copy files across multiple servers,
# batch-remove files remotely, batch-download remote files, and batch-create symbolic
# links.
#
# Typically used to automate a build release or retrieve log files for analysis.
#
# WinSCP <http://winscp.net/eng/index.php> is required, with its installation directory
# added to Windows' search path (environment variable PATH). 
#
# References:
# see http://winscp.net/eng/docs/scripting
# see http://winscp.net/eng/docs/scripts
# see http://effbot.org/librarybook/os.htm
# http://wiki/index.php?title=Linux_File_Copying_Procedures

import os, subprocess, datetime

# list of destination servers
SERVERS = ['<USER>@<SERVER>']

# added limit to transfer speed '-speed=<kibps>' (only available from version 4.1), as per
# http://wiki/index.php?title=Linux_File_Copying_Procedures
WINSCP_TEMPLATES = {'put':'%s /command "option batch on" "option confirm off" "put -speed=2000 %s %s/" "exit"',
                    'get':'%s /command "option batch on" "option confirm off" "get -speed=2000 %s/%s" "exit"',
                    'rm':'%s /command "option batch on" "option confirm off" "rm %s/%s" "exit"',
                    'ln':'%s /command "option batch on" "option confirm off" "ln ~/%s/%s <SYM_LINK>" "exit"'} # customize SYM_LINK

LOCAL_FILENAME = '<LOCAL_FILENAME>'
LOCAL_PATH = '<LOCAL_PATH>'
LOCAL_LOG_FILENAME = 'scp_batch.log'
REMOTE_PATH = '<REMOTE_PATH>'
REMOTE_FILENAME = '<REMOTE_FILENAME>'

def local_path():
    os.chdir(LOCAL_PATH)
    print os.getcwd()

def command(op, server, dirs, filename):
    return WINSCP_TEMPLATES[op] % (server, dirs, filename, )

# SCP given command={operation, server, directory, filename} template string
def scp_to_server(command):
    print 'Executing: winscp %s' % (command, )
    try:
        # argument list starts at 0, not 1
        #os.execlp("winscp", "winscp", command)
        args = "winscp.exe %s" % (command,)
        # os.execlp delegates control to spawned process, which doesn't work in a loop
        output = subprocess.Popen(args, shell=True, stdout=open(LOCAL_LOG_FILENAME, 'w'), stderr=subprocess.STDOUT)
        print output
    except:
        print 'Problem executing SCP'

def scp_to_servers(op, dirs, filename):
    for server in SERVERS:
        scp_to_server(command(op, server, dirs, filename))

local_path()
# put operations reverse dir/filename order
#op = command('put', '<USER>@<SERVER>', LOCAL_FILENAME, REMOTE_PATH)
#op = command('get', '<USER>@<SERVER>', REMOTE_PATH, REMOTE_FILENAME)
#op = command('rm', '<USER>@<SERVER>', REMOTE_PATH, REMOTE_FILENAME)
#op = command('ln', '<USER>@<SERVER>', REMOTE_PATH, REMOTE_FILENAME)
#scp_to_server(op)

# put operations reverse dir/filename order
#scp_to_servers('put', LOCAL_FILENAME, REMOTE_PATH)
#scp_to_servers('get', REMOTE_PATH, REMOTE_FILENAME)
#scp_to_servers('rm', REMOTE_PATH, REMOTE_FILENAME)
#scp_to_servers('ln', REMOTE_PATH, REMOTE_FILENAME)

