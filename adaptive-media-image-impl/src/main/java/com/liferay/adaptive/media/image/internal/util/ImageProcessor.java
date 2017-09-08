/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.adaptive.media.image.internal.util;

import com.liferay.adaptive.media.exception.AMRuntimeException;
import com.liferay.adaptive.media.image.configuration.AMImageConfiguration;
import com.liferay.adaptive.media.image.configuration.AMImageConfigurationEntry;
import com.liferay.adaptive.media.image.internal.processor.util.TiffOrientationTransformer;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.image.ImageToolUtil;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;

import java.awt.image.RenderedImage;

import java.io.InputStream;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(
	configurationPid = "com.liferay.adaptive.media.image.configuration.AMImageConfiguration",
	immediate = true, service = ImageProcessor.class
)
public class ImageProcessor {

	public boolean isMimeTypeSupported(String mimeType) {
		return ArrayUtil.contains(
			_amImageConfiguration.supportedMimeTypes(), mimeType);
	}

	public RenderedImage scaleImage(
		FileVersion fileVersion,
		AMImageConfigurationEntry amImageConfigurationEntry) {

		try {
			RenderedImage renderedImage = _tiffOrientationTransformer.transform(
				() -> _getInputStream(fileVersion));

			Map<String, String> properties =
				amImageConfigurationEntry.getProperties();

			int maxHeight = GetterUtil.getInteger(properties.get("max-height"));
			int maxWidth = GetterUtil.getInteger(properties.get("max-width"));

			return ImageToolUtil.scale(renderedImage, maxHeight, maxWidth);
		}
		catch (PortalException pe) {
			throw new AMRuntimeException.IOException(pe);
		}
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_amImageConfiguration = ConfigurableUtil.createConfigurable(
			AMImageConfiguration.class, properties);
	}

	private InputStream _getInputStream(FileVersion fileVersion) {
		try {
			return fileVersion.getContentStream(false);
		}
		catch (PortalException pe) {
			throw new AMRuntimeException.IOException(pe);
		}
	}

	private volatile AMImageConfiguration _amImageConfiguration;

	@Reference
	private TiffOrientationTransformer _tiffOrientationTransformer;

}