from __future__ import print_function
import sys
import os

def parse_pdbqt(pdbqt_content):
    """Parse PDBQT content and return a list of lines."""
    return [line for line in pdbqt_content.split('\n') if line.startswith(('ATOM', 'HETATM'))]

def convert_line_to_pdb(line):
    """Convert a single PDBQT line to PDB format."""
    # Extract relevant information
    record_name = line[:6].strip()
    atom_number = int(line[6:11])
    atom_name = line[12:16].strip()
    alt_loc = line[16].strip()
    res_name = line[17:20].strip()
    chain_id = line[21].strip()
    res_number = int(line[22:26])
    x = float(line[30:38])
    y = float(line[38:46])
    z = float(line[46:54])
    occupancy = float(line[54:60]) if line[54:60].strip() else 1.0
    temp_factor = float(line[60:66]) if line[60:66].strip() else 0.0
    element = line[76:78].strip()

    # Format PDB line
    pdb_line = "{0:<6}{1:>5} {2:<4}{3:1}{4:>3} {5:1}{6:>4}    {7:>8.3f}{8:>8.3f}{9:>8.3f}{10:>6.2f}{11:>6.2f}          {12:>2}  ".format(
        record_name, atom_number, atom_name, alt_loc, res_name, chain_id, res_number,
        x, y, z, occupancy, temp_factor, element
    )
    return pdb_line

def pdbqt_to_pdb(pdbqt_content):
    """Convert PDBQT content to PDB format."""
    pdbqt_lines = parse_pdbqt(pdbqt_content)
    pdb_lines = [convert_line_to_pdb(line) for line in pdbqt_lines]
    return '\n'.join(pdb_lines)

def convert_file(input_path, output_path):
    try:
        # Read input file
        with open(input_path, 'r') as f:
            pdbqt_content = f.read()

        # Convert PDBQT to PDB
        pdb_content = pdbqt_to_pdb(pdbqt_content)

        # Write output file
        with open(output_path, 'w') as f:
            f.write(pdb_content)

        print("Conversion complete. PDB file saved as: {}".format(output_path))

    except Exception as e:
        print("An error occurred: {}".format(str(e)))
        print("Please make sure you've provided valid file paths and the input file is a valid PDBQT file.")

def main():
    if len(sys.argv) != 3:
        print("Usage: python converter.py <input_pdbqt_file> <output_pdb_file>")
        sys.exit(1)

    input_path = sys.argv[1]
    output_path = sys.argv[2]

    convert_file(input_path, output_path)

if __name__ == "__main__":
    main()