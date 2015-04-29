package co.codewizards.cloudstore.ls.rest.server;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.codewizards.cloudstore.ls.rest.server.auth.AuthFilter;
import co.codewizards.cloudstore.ls.rest.server.service.RepoInfoService;
import co.codewizards.cloudstore.ls.rest.server.service.TestService;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@ApplicationPath("LocalServerRest")
public class LocalServerRest extends ResourceConfig {
	private static final Logger logger = LoggerFactory.getLogger(LocalServerRest.class);

	static {
		logger.debug("<static_init>: Class loaded.");
	}

    {
		logger.debug("<init>: Instance created.");

		registerClasses(
				// BEGIN services
				RepoInfoService.class,
				TestService.class,
				// END services

				// BEGIN providers
				// providers are not services (they are infrastructure), but they are registered the same way.
				AuthFilter.class,
//				GZIPReaderInterceptor.class, // compression not needed - local (same machine)
//				GZIPWriterInterceptor.class, // compression not needed - local (same machine)
				CloudStoreJaxbContextResolver.class,
				DefaultExceptionMapper.class
				// END providers
				);

		register(new LocalServerRestBinder());
	}
}