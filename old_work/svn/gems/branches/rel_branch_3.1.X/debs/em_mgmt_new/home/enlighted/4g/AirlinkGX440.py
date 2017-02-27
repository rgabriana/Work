import paramiko
import time

# Uncomment this line if you want to turn on SSH debugging
#paramiko.common.logging.basicConfig(level=paramiko.common.DEBUG)

class AirlinkGX440:
    def __init__(self, host, username, password):
        self._host = host
        self._user = username
        self._pass = password

    def reboot(self):
        # ATZ is the reboot AT command for the GX440
        return self._runCommand("ATZ\n")

    def _runCommand(self, command):
        # Instantiate an SSH client
        cli = paramiko.SSHClient()

        # Auto add any missing host keys
        cli.set_missing_host_key_policy(paramiko.AutoAddPolicy())

        # Connect to the gateway
        cli.connect(self._host, username=self._user, password=self._pass)

        # Set up the channel by invoking an interactive shell and turning blocking IO off
        chan = cli.invoke_shell()
        chan.setblocking(0)

        # Wait until the SSH channel is ready to receive data or for 30 seconds. Whichever comes first.
        outp = ''
        ready = False
        loopCounter = 0
        while(chan.recv_ready() is not True and loopCounter < 300):
            time.sleep(0.1)
            loopCounter += 1

        # If we timed out waiting for the channel to be receive ready...
        if(loopCounter >=300):
            raise RuntimeError("Channel did not become ready in time")

        # Read the channel looking for "OK\n" which is the string that the GX440 prints when it is ready
        # to receive a command
        loopCounter = 0
        while(chan.recv_ready() and loopCounter < 300):
            buf=chan.recv(1)
            outp += buf
            if (outp[-4:] == "OK\r\n"):
                ready = True
                break
            loopCounter += 1

        # If we timed out waiting for the OK...
        if (ready is not True):
            raise RuntimeError("Nothing to read on channel anymore, but prompt not found")

        # Since this method can take a list of commands or a command string, we'll make a list of one
        # element if a string was passed in. That way logic below only has to worry about processing
        # one data type.
        if(isinstance(command, str)):
            cmdList = [command]
        else:
            cmdList = command

        # Iterate through the list of commands
        outp=''
        for cmd in cmdList:
            # Send the command
            chan.sendall(cmd)

            # Gather output waiting up to 10 seconds
            loopCounter = 0
            while(chan.exit_status_ready() is not True and chan.get_transport().is_active() is True and loopCounter < 100):
                time.sleep(0.1)
                loopCounter += 1

                while(chan.recv_ready()):
                    buf=chan.recv(1024)
                    outp += buf

        # Close the SSH channel
        chan.close()

        # Return all gathered output
        return outp
