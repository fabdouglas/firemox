/*
 *   Firemox is a turn based strategy simulator
 *   Copyright (C) 2003-2007 Fabrice Daugan
 *
 *   This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 *   This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 *
 *   You should have received a copy of the GNU General Public License along  
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sf.firemox.chart;

import java.awt.Color;
import java.awt.Paint;
import java.util.Map;

import net.sf.firemox.chart.datasets.BarDataset;
import net.sf.firemox.chart.datasets.Dataset;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.Rotation;

/**
 * Applicable filters for charts.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public enum ChartFilter {

	/**
	 * To filter the color.
	 */
	color,

	/**
	 * To filter the type.
	 */
	type,

	/**
	 * To filter the property.
	 */
	property,

	/**
	 * To filter the mana cost.
	 */
	manacost;

	/**
	 * @param dataSet
	 *          the data set associated to this filter.
	 * @param painterMapper
	 *          the optional paint mapper to associate to this filter.
	 * @return the plot associated to this filter.
	 */
	public Plot createPlot(Dataset dataSet, Map<Integer, Paint> painterMapper) {
		switch (this) {
		case color:
			return new MPiePlot((PieDataset) dataSet, painterMapper);
		case type:
		case property:
			return new MBarPlot((BarDataset) dataSet);
		case manacost:
		default:
			return new MAreaPlot((DefaultCategoryDataset) dataSet);
		}
	}

	/**
	 * This custom PiePlot selects Paint values based on section data.
	 */
	private class MPiePlot extends PiePlot3D {
		private final Map<Integer, Paint> painterMapper;

		MPiePlot(PieDataset dataset, Map<Integer, Paint> painterMapper) {
			super(dataset);
			this.painterMapper = painterMapper;
			this.setLabelGenerator(null);
			this.setBackgroundPaint(Color.LIGHT_GRAY);
			this.setOutlinePaint(Color.BLACK);
			this.setStartAngle(290D);
			this.setDirection(Rotation.CLOCKWISE);
			this.setForegroundAlpha(0.5F);
			this.setToolTipGenerator(new StandardPieToolTipGenerator());
			this.setInsets(new RectangleInsets(0.0, 5.0, 5.0, 5.0));
		}

		@Override
		public Paint getSectionPaint(int section) {
			if (getDataset().getItemCount() == 0)
				return Color.LIGHT_GRAY;
			if (painterMapper == null)
				return super.getSectionPaint(section);
			Paint paint = painterMapper.get(((IChartKey) getDataset().getKeys().get(
					section)).getIntegerKey());
			if (paint == null)
				paint = painterMapper.get(DEFAULT_KEY);
			if (paint == null)
				paint = super.getSectionPaint(section);
			return paint;
		}
	}

	/**
	 * This custom PiePlot selects Paint values based on section data.
	 */
	private class MBarPlot extends CategoryPlot {
		MBarPlot(BarDataset dataset) {
			super(dataset, new CategoryAxis(null), new NumberAxis("Amount"),
					new BarRenderer());
			this.getRangeAxis().setStandardTickUnits(
					NumberAxis.createIntegerTickUnits());

			CategoryAxis categoryaxis = getDomainAxis();
			categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
			this.setRangeGridlinePaint(Color.WHITE);
			this.setBackgroundPaint(Color.LIGHT_GRAY);
			this.setOutlinePaint(Color.BLACK);
			this.setForegroundAlpha(0.5F);
		}
	}

	/**
	 * This custom PiePlot selects Paint values based on area.
	 */
	private class MAreaPlot extends CategoryPlot {
		MAreaPlot(DefaultCategoryDataset dataset) {
			super(dataset, new CategoryAxis(LanguageManager.getString("manacost")),
					new NumberAxis(LanguageManager.getString("amount")),
					new AreaRenderer());
			((AreaRenderer) getRenderer()).setFillPaint(Color.BLUE);
			((AreaRenderer) getRenderer()).setSeriesFillPaint(0, Color.BLUE);
			((AreaRenderer) getRenderer()).setSeriesOutlinePaint(0, Color.BLUE);
			((AreaRenderer) getRenderer()).setPaint(Color.BLUE);
			((AreaRenderer) getRenderer()).setSeriesFillPaint(0, Color.BLUE);
			this.setForegroundAlpha(0.5F);
			this.setDomainGridlinesVisible(true);
			this.setDomainGridlinePaint(Color.WHITE);
			this.setRangeGridlinesVisible(true);
			this.setRangeGridlinePaint(Color.WHITE);
			CategoryAxis categoryaxis = getDomainAxis();
			categoryaxis.setCategoryMargin(0.0);
			categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
			categoryaxis.setLowerMargin(0.0D);
			categoryaxis.setUpperMargin(0.0D);
			// category axis.addCategoryLabelToolTip("Type 1", "The first type.");
			NumberAxis numberaxis = (NumberAxis) getRangeAxis();
			numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			numberaxis.setLabelAngle(0.0D);
			this.setOrientation(PlotOrientation.VERTICAL);
			this.setBackgroundPaint(Color.LIGHT_GRAY);
			this.setOutlinePaint(Color.BLACK);
			this.setForegroundAlpha(0.5F);
		}
	}

	/**
	 * 
	 */
	public static final Integer DEFAULT_KEY = Integer.MAX_VALUE;

	/**
	 * A new Data set instance.
	 * 
	 * @param dataProvider
	 * @return a new data set instance.
	 */
	public Dataset createDataSet(IDataProvider dataProvider) {
		switch (this) {
		case color:
			return new net.sf.firemox.chart.datasets.PieDataset(dataProvider,
					this);
		case type:
		case property:
			return new net.sf.firemox.chart.datasets.BarDataset(dataProvider,
					this);
		case manacost:
		default:
			return new net.sf.firemox.chart.datasets.CategoryDataset(
					dataProvider, this);
		}
	}

	/**
	 * Return the title of this chart.
	 * 
	 * @return the title of this chart.
	 */
	public String getTitle() {
		switch (this) {
		case color:
			return LanguageManager.getString("colors");
		case type:
			return LanguageManager.getString("types");
		case property:
			return LanguageManager.getString("properties");
		case manacost:
		default:
			return LanguageManager.getString("manacost");
		}
	}

}
