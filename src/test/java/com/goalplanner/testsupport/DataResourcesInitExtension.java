package com.goalplanner.testsupport;

import com.goalplanner.data.BossKillData;
import com.goalplanner.data.DiaryRequirements;
import com.google.gson.Gson;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Primes the bundled requirement tables once, before any test class runs.
 *
 * <p>In production the plugin loads {@link DiaryRequirements} and
 * {@link BossKillData} from their JSON resources using the client's
 * <em>injected</em> {@link Gson} (the plugin-hub forbids plugins from
 * constructing their own Gson). Tests have no injector, so this extension
 * primes the same static tables with a plain {@code new Gson()} — allowed
 * here because the hub's forbidden-API check scans main source only.
 *
 * <p>Auto-registered for every test via the JUnit Jupiter {@code ServiceLoader}
 * mechanism: it is listed in
 * {@code META-INF/services/org.junit.jupiter.api.extension.Extension} and
 * {@code junit-platform.properties} enables
 * {@code junit.jupiter.extensions.autodetection.enabled}. No per-class wiring
 * is needed, and the {@code init} calls are idempotent.
 */
public final class DataResourcesInitExtension implements BeforeAllCallback
{
	@Override
	public void beforeAll(ExtensionContext context)
	{
		Gson gson = new Gson();
		DiaryRequirements.init(gson);
		BossKillData.init(gson);
	}
}
