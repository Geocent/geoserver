package org.geotools.xacml.geoxacml.cond.initializers;

import org.geotools.xacml.geoxacml.cond.*;
import org.herasaf.xacml.core.function.Function;
import org.herasaf.xacml.core.simplePDP.initializers.jaxb.typeadapter.xacml20.functions.AbstractFunctionsJaxbTypeAdapterInitializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoXACMLDefaultFunctionsJaxbInitializer extends
        AbstractFunctionsJaxbTypeAdapterInitializer {

        /**
         * {@inheritDoc}<br />
         * <b>This implementation:</b><br />
         * Instantiates all default XACML 2.0 {@link Function Functions}.
         */
        @Override
        protected Map<String, Function> createTypeInstances() {
            List<Function> arithmeticFunctions = createGeoXACMLFunctions();

            @SuppressWarnings("unchecked")
            List<Function> allFunctions = concatenate(arithmeticFunctions);

            Map<String, Function> instancesMap = new HashMap<String, Function>();
            for (Function function : allFunctions) {
                instancesMap.put(function.toString(), function);
            }

            return instancesMap;
        }

        private List<Function> createGeoXACMLFunctions() {
            List<Function> functions = createInstances(
                    ConvertToMetre.class,
                    ConvertToSquareMetre.class,
                    GeometryArea.class,
                    GeometryBoundary.class,
                    GeometryBuffer.class,
                    GeometryCentroid.class,
                    GeometryContains.class,
                    GeometryConvexHull.class,
                    GeometryCrosses.class,
                    GeometryDifference.class,
                    GeometryDisjoint.class,
                    GeometryDistance.class,
                    GeometryEquals.class,
                    GeometryIntersection.class,
                    GeometryIntersects.class,
                    GeometryIsClosed.class,
                    GeometryIsSimple.class,
                    GeometryIsValid.class,
                    GeometryIsWithinDistance.class,
                    GeometryLength.class,
                    GeometryOverlaps.class,
                    GeometrySymDifference.class,
                    GeometryTouches.class,
                    GeometryUnion.class,
                    GeometryWithin.class
            );
            return functions;
        }

        private <T> List<T> concatenate(List<T>... lists) {
            List<T> allFunctions = new ArrayList<T>();

            for (List<T> list : lists) {
                allFunctions.addAll(list);
            }

            return allFunctions;
        }
}
