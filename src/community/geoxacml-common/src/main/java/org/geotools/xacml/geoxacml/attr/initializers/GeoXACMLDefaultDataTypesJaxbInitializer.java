package org.geotools.xacml.geoxacml.attr.initializers;

import org.geotools.xacml.geoxacml.attr.GeometryDataTypeAttribute;
import org.herasaf.xacml.core.dataTypeAttribute.DataTypeAttribute;
import org.herasaf.xacml.core.simplePDP.initializers.jaxb.typeadapter.xacml20.datatypes.AbstractDataTypesJaxbTypeAdapterInitializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoXACMLDefaultDataTypesJaxbInitializer extends
        AbstractDataTypesJaxbTypeAdapterInitializer {

        /**
         * {@inheritDoc}<br />
         * <b>This implementation:</b><br />
         * Instantiates all default XACML 2.0 {@link DataTypeAttribute
         * DataTypeAttributes}.
         */
        @Override
        protected Map<String, DataTypeAttribute<?>> createTypeInstances() {

            List<DataTypeAttribute<?>> instances = createInstances(
                    GeometryDataTypeAttribute.class
            );

            Map<String, DataTypeAttribute<?>> instancesMap = new HashMap<String, DataTypeAttribute<?>>();
            for (DataTypeAttribute<?> dataTypeAttribute : instances) {
                instancesMap.put(dataTypeAttribute.getDatatypeURI(),
                        dataTypeAttribute);
            }

            return instancesMap;
        }
}
