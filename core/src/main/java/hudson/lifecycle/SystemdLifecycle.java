package hudson.lifecycle;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * {@link Lifecycle} that delegates its responsibility to {@code systemd(1)}.
 *
 * @author Basil Crow
 */
@Restricted(NoExternalUse.class)
@Extension(optional = true)
public class SystemdLifecycle extends ExitLifecycle {

    private static final Logger LOGGER = Logger.getLogger(SystemdLifecycle.class.getName());

    interface Systemd extends Library {
        Systemd INSTANCE = Native.load("systemd", Systemd.class);

        int sd_notify(int unset_environment, String state) throws LastErrorException;
    }

    @Override
    public void onReady() {
        super.onReady();
        notify("READY=1");
    }

    @Override
    public void onReload(@NonNull String user, @CheckForNull String remoteAddr) {
        super.onReload(user, remoteAddr);
        notify("RELOADING=1");
    }

    @Override
    public void onStop(@NonNull String user, @CheckForNull String remoteAddr) {
        super.onStop(user, remoteAddr);
        notify("STOPPING=1");
    }

    @Override
    public void onStatusUpdate(String status) {
        super.onStatusUpdate(status);
        notify(String.format("STATUS=%s", status));
    }

    private static synchronized void notify(String message) {
        try {
            Systemd.INSTANCE.sd_notify(0, message);
        } catch (LastErrorException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
    }
}
