/*
 * Copyright 2022 Johan Fylling, Anders Eknert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package by.borge.jarl;

import by.borge.jarl.internal.IntermediateRepresentation;
import by.borge.jarl.internal.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Jarl {
    private final IntermediateRepresentation ir;

    private Jarl(IntermediateRepresentation ir) {
        this.ir = ir;
    }

    public Plan getPlan(String entryPoint) {
        var plan = ir.getPlan(entryPoint);
        if (plan == null) {
            throw new IllegalArgumentException(String.format("No plan found for entry-point: %s", entryPoint));
        }
        return new Plan(plan);
    }

    public static class Builder {
        private final File irFile;
        private boolean strict = false;

        public Builder(File irFile) {
            this.irFile = irFile;
        }

        public Builder strictBuiltinErrors(boolean strict) {
            this.strict = strict;
            return this;
        }

        public Jarl build() throws IOException {
            var rawIr = Files.readString(irFile.toPath());
            var ir = Parser.parse(rawIr)
                    .withStrictBuiltinErrors(strict);
            return new Jarl(ir);
        }
    }
}
