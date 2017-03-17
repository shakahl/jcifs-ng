/* jcifs smb client library in Java
 * Copyright (C) 2000  "Michael B. Allen" <jcifs at samba dot org>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package jcifs.smb;


import java.io.IOException;


class TransactNamedPipeOutputStream extends SmbPipeOutputStream {

    private SmbPipeHandle handle;
    private byte[] tmp = new byte[1];
    private boolean dcePipe;


    TransactNamedPipeOutputStream ( SmbPipeHandle handle, SmbTreeHandleImpl th ) throws SmbException {
        super(handle, th);
        this.handle = handle;
        this.dcePipe = ( handle.getPipeType() & SmbNamedPipe.PIPE_TYPE_DCE_TRANSACT ) == SmbNamedPipe.PIPE_TYPE_DCE_TRANSACT;
    }


    @Override
    public void write ( int b ) throws IOException {
        this.tmp[ 0 ] = (byte) b;
        write(this.tmp, 0, 1);
    }


    @Override
    public void write ( byte[] b ) throws IOException {
        write(b, 0, b.length);
    }


    @Override
    public void write ( byte[] b, int off, int len ) throws IOException {
        if ( len < 0 ) {
            len = 0;
        }

        try ( SmbFileHandleImpl fh = this.handle.ensureOpen();
              SmbTreeHandleImpl th = fh.getTree() ) {
            TransTransactNamedPipe req = new TransTransactNamedPipe(th.getConfig(), fh.getFid(), b, off, len);
            if ( this.dcePipe ) {
                req.maxDataCount = 1024;
            }
            th.send(req, new TransTransactNamedPipeResponse(th.getConfig(), this.handle));
        }
    }
}
