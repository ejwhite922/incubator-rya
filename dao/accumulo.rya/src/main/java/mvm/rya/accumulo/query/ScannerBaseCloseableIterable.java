package mvm.rya.accumulo.query;

/*
 * #%L
 * mvm.rya.accumulo.rya
 * %%
 * Copyright (C) 2014 Rya
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Preconditions;
import org.apache.accumulo.core.client.ScannerBase;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.calrissian.mango.collect.AbstractCloseableIterable;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Date: 1/30/13
 * Time: 2:15 PM
 */
public class ScannerBaseCloseableIterable extends AbstractCloseableIterable<Map.Entry<Key, Value>> {

    protected ScannerBase scanner;

    public ScannerBaseCloseableIterable(ScannerBase scanner) {
        Preconditions.checkNotNull(scanner);
        this.scanner = scanner;
    }

    @Override
    protected void doClose() throws IOException {
        scanner.close();
    }

    @Override
    protected Iterator<Map.Entry<Key, Value>> retrieveIterator() {
        return scanner.iterator();
    }
}
