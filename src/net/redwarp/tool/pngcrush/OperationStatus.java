/*
 * Copryright (C) 2012 Redwarp
 * 
 * This file is part of PNGCrush Wrapper.
 * PNGCrush Wrapper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * PNGCrush Wrapper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with PNGCrush Wrapper.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.redwarp.tool.pngcrush;

import java.io.File;

public class OperationStatus {
		public static enum Status {
			PENDING, INPROGRESS, ERROR, FINISH;
		}

		private File file;
		private Status status;
		private float reduction;

		public OperationStatus(File f) {
			this.file = f;
			this.status = Status.PENDING;
			this.reduction = 0f;
		}

		public File getFile() {
			return file;
		}

		public synchronized Status getStatus() {
			return status;
		}

		public synchronized void setStatus(Status status) {
			this.status = status;
		}

		public void setReduction(float reduction) {
			this.reduction = reduction;
		}

		public float getReduction() {
			return reduction;
		}
	}