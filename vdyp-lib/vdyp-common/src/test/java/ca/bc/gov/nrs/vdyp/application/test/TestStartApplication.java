package ca.bc.gov.nrs.vdyp.application.test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.application.VdypStartApplication;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.FileSystemFileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.control.NonFipControlParser;
import ca.bc.gov.nrs.vdyp.model.BaseVdypSpecies.Builder;

public class TestStartApplication extends VdypStartApplication<TestPolygon, TestLayer, TestSpecies, TestSite> {

	boolean realInit;

	public TestStartApplication(Map<String, Object> controlMap, boolean realInit) {
		this.setControlMap(controlMap);
		this.realInit = realInit;
	}

	@Override
	public void init(FileSystemFileResolver resolver, String... controlFilePaths)
			throws IOException, ResourceParseException {
		if (realInit) {
			super.init(resolver, controlFilePaths);
		}
	}

	@Override
	public void init(FileSystemFileResolver resolver, Map<String, Object> controlMap) throws IOException {
		if (realInit) {
			super.init(resolver, controlMap);
		}
	}

	@Override
	protected NonFipControlParser getControlFileParser() {
		return new NonFipControlParser() {

			@Override
			protected List<ControlMapValueReplacer<Object, String>> inputFileParsers() {
				return List.of();
			}

			@Override
			protected List<ControlKey> outputFileParsers() {
				return List.of();
			}

			@Override
			protected VdypApplicationIdentifier getProgramId() {
				return VdypApplicationIdentifier.VRI_START;
			}

		};
	}

	@Override
	public void process() throws ProcessingException {
		// Do Nothing
	}

	@Override
	public VdypApplicationIdentifier getId() {
		return VdypApplicationIdentifier.VRI_START;
	}

	@Override
	protected TestSpecies copySpecies(TestSpecies toCopy, Consumer<Builder<TestSpecies, TestSite, ?>> config) {
		return null;
	}

	@Override
	protected Optional<TestSite> getPrimarySite(TestLayer layer) {
		return Utils.optSafe(layer.getSites().values().iterator().next());
	}

	@Override
	protected float getYieldFactor(TestPolygon polygon) {
		// TODO Auto-generated method stub
		return 1;
	}

}